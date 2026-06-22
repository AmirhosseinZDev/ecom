import { Component } from '@angular/core';

@Component({
  selector: 'app-home',
  template: `
    <main class="home">
      <h1>فروشگاه</h1>
      <p>به فروشگاه آنلاین خوش آمدید.</p>
    </main>
  `,
  styles: `
    .home {
      min-height: 100dvh;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 0.5rem;
      font-family: system-ui, sans-serif;
    }

    h1 {
      margin: 0;
      font-size: 2rem;
    }

    p {
      margin: 0;
      color: #555;
    }
  `
})
export class Home {}
